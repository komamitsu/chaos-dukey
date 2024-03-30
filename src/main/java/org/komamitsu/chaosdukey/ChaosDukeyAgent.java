package org.komamitsu.chaosdukey;

import java.lang.instrument.Instrumentation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class ChaosDukeyAgent {
  static final ChaosDukeyInterceptor.WaitMode DEFAULT_WAIT_MODE =
      ChaosDukeyInterceptor.WaitMode.RANDOM;
  static final int DEFAULT_PERCENTAGE = 5;
  static final int DEFAULT_MAX_DELAY_MILLIS = 100;
  final ElementMatcher<TypeDefinition> typeMatcher;
  final ElementMatcher<MethodDescription> methodMatcher;
  final ChaosDukeyInterceptor.WaitMode waitMode;
  final int percentage;
  final int maxDelayMillis;
  final boolean debug;

  ChaosDukeyAgent(
      ElementMatcher<TypeDefinition> typeMatcher,
      ElementMatcher<MethodDescription> methodMatcher,
      ChaosDukeyInterceptor.WaitMode waitMode,
      int percentage,
      int maxDelayMillis,
      boolean debug) {

    this.typeMatcher = typeMatcher;
    this.methodMatcher = methodMatcher;
    this.waitMode = waitMode;
    this.percentage = percentage;
    this.maxDelayMillis = maxDelayMillis;
    this.debug = debug;
  }

  @Override
  public String toString() {
    return "ChaosDukeyAgent{"
        + "typeMatcher="
        + typeMatcher
        + ", methodMatcher="
        + methodMatcher
        + ", waitMode="
        + waitMode
        + ", percentage="
        + percentage
        + ", maxDelayMillis="
        + maxDelayMillis
        + ", debug="
        + debug
        + '}';
  }

  static ChaosDukeyAgent parseArguments(String arguments) {
    ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.any();
    ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.any();
    ChaosDukeyInterceptor.WaitMode waitMode = DEFAULT_WAIT_MODE;
    int percentage = DEFAULT_PERCENTAGE;
    int maxDelayMillis = DEFAULT_MAX_DELAY_MILLIS;
    boolean debug = false;

    if (arguments != null) {
      for (String kv : arguments.split(",")) {
        String[] tokens = kv.trim().split("=");
        if (tokens.length == 2) {
          String k = tokens[0].trim();
          String v = tokens[1].trim();
          if (k.equals("typeNamePattern")) {
            typeMatcher = ElementMatchers.nameMatches(v);
          } else if (k.equals("methodNamePattern")) {
            methodMatcher = ElementMatchers.nameMatches(v);
          } else if (k.equals("waitMode")) {
            waitMode = ChaosDukeyInterceptor.WaitMode.valueOf(v);
          } else if (k.equals("percentage")) {
            percentage = Integer.parseInt(v);
          } else if (k.equals("maxDelayMillis")) {
            maxDelayMillis = Integer.parseInt(v);
          } else if (k.equals("debug")) {
            debug = Boolean.valueOf(v);
          } else {
            System.err.println("Unexpected option: " + v);
          }
        }
      }
    }

    return new ChaosDukeyAgent(
        typeMatcher, methodMatcher, waitMode, percentage, maxDelayMillis, debug);
  }

  public static void premain(String arguments, Instrumentation instrumentation) {
    ChaosDukeyAgent agent = parseArguments(arguments);

    ChaosDukeyInterceptor interceptor =
        new ChaosDukeyInterceptor(agent.waitMode, agent.percentage, agent.maxDelayMillis);
    AgentBuilder agentBuilder =
        new AgentBuilder.Default()
            .type(agent.typeMatcher)
            .transform(
                (builder, type, classLoader, module, protectionDomain) ->
                    builder
                        .method(agent.methodMatcher)
                        .intercept(MethodDelegation.to(interceptor)));
    if (agent.debug) {
      agentBuilder = agentBuilder.with(AgentBuilder.Listener.StreamWriting.toSystemOut());
    }

    agentBuilder.installOn(instrumentation);
  }
}
