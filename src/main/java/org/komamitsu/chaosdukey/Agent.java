package org.komamitsu.chaosdukey;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

public final class Agent {
  static Properties propertiesFromArguments(String arguments) {
    Properties properties = new Properties();
    if (arguments != null) {
      for (String kv : arguments.split(",")) {
        String[] tokens = kv.trim().split("=");
        if (tokens.length == 2) {
          String k = tokens[0].trim();
          String v = tokens[1].trim();
          properties.put(k, v);
        } else {
          throw new IllegalArgumentException(
              "Parameters should be in the format <key>=<value>. But, an invalid parameter was passed: "
                  + kv);
        }
      }
    }
    return properties;
  }

  public static void premain(String arguments, Instrumentation instrumentation) throws IOException {
    Config config = new Config.Loader().load(propertiesFromArguments(arguments));

    {
      InterceptorForDelay interceptor = new InterceptorForDelay(config.delayConfig, config.debug);
      AgentBuilder agentBuilder =
          new AgentBuilder.Default()
              .type(config.delayConfig.typeMatcher)
              .transform(
                  (builder, type, classLoader, module, protectionDomain) ->
                      builder
                          .method(config.delayConfig.methodMatcher)
                          .intercept(MethodDelegation.to(interceptor)));
      if (config.debug) {
        agentBuilder = agentBuilder.with(AgentBuilder.Listener.StreamWriting.toSystemError());
      }
      agentBuilder.installOn(instrumentation);
    }
    {
      InterceptorForFailure interceptor =
          new InterceptorForFailure(config.failureConfig, config.debug);
      AgentBuilder agentBuilder =
          new AgentBuilder.Default()
              .type(config.failureConfig.typeMatcher)
              .transform(
                  (builder, type, classLoader, module, protectionDomain) ->
                      builder
                          .method(config.failureConfig.methodMatcher)
                          .intercept(MethodDelegation.to(interceptor)));
      if (config.debug) {
        agentBuilder = agentBuilder.with(AgentBuilder.Listener.StreamWriting.toSystemError());
      }
      agentBuilder.installOn(instrumentation);
    }
  }
}
