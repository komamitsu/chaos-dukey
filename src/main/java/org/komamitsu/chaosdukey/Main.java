package org.komamitsu.chaosdukey;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;

public final class Main {
  static Config configFromArguments(String arguments) throws IOException {
    Properties properties = new Properties();
    if (arguments != null) {
      for (String kv : arguments.split(",")) {
        String[] tokens = kv.trim().split("=");
        if (tokens.length == 2) {
          String k = tokens[0].trim();
          String v = tokens[1].trim();
          properties.put(k, v);
        }
        else {
          throw new IllegalArgumentException("Parameters should be in the format <key>=<value>. But, an invalid parameter was passed: " + kv);
        }
      }
    }
    return new Config.Loader().load(properties);
  }

  public static void premain(String arguments, Instrumentation instrumentation) throws IOException {
    Config config= configFromArguments(arguments);

    Interceptor interceptor =
        new Interceptor(
            config.delayConfig.waitMode, config.delayConfig.ppm, config.delayConfig.maxDelayMillis, config.debug);
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
}
