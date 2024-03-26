package org.komamitsu.chaosdukey;

import java.lang.instrument.Instrumentation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class ChaosDukeyAgent {
  static final int DEFAULT_PERCENTAGE = 5;
  static final int DEFAULT_MAX_DELAY_MILLIS = 100;
  final ElementMatcher<TypeDefinition> typeMatcher;
  final ElementMatcher<MethodDescription> methodMatcher;
  final int percentage;
  final int maxDelayMillis;

  ChaosDukeyAgent(
      ElementMatcher<TypeDefinition> typeMatcher,
      ElementMatcher<MethodDescription> methodMatcher,
      int percentage,
      int maxDelayMillis) {

    this.typeMatcher = typeMatcher;
    this.methodMatcher = methodMatcher;
    this.percentage = percentage;
    this.maxDelayMillis = maxDelayMillis;
  }

  @Override
  public String toString() {
    return "ChaosDukeyAgent{"
        + "typeMatcher="
        + typeMatcher
        + ", methodMatcher="
        + methodMatcher
        + ", percentage="
        + percentage
        + ", maxDelayMillis="
        + maxDelayMillis
        + '}';
  }

  static ChaosDukeyAgent parseArguments(String arguments) {
    ElementMatcher<TypeDefinition> typeMatcher = ElementMatchers.any();
    ElementMatcher<MethodDescription> methodMatcher = ElementMatchers.any();
    int percentage = DEFAULT_PERCENTAGE;
    int maxDelayMillis = DEFAULT_MAX_DELAY_MILLIS;

    if (arguments != null) {
      for (String kv : arguments.split(",")) {
        String[] tokens = kv.trim().split("=");
        if (tokens.length == 2) {
          String k = tokens[0].trim();
          String v = tokens[1].trim();
          if (k.equals("typeNamePattern")) {
            typeMatcher = ElementMatchers.nameMatches(v.trim());
          } else if (k.equals("methodNamePattern")) {
            methodMatcher = ElementMatchers.nameMatches(v.trim());
          } else if (k.equals("percentage")) {
            percentage = Integer.parseInt(v);
          } else if (k.equals("maxDelayMillis")) {
            maxDelayMillis = Integer.parseInt(v);
          }
        }
      }
    }

    return new ChaosDukeyAgent(typeMatcher, methodMatcher, percentage, maxDelayMillis);
  }

  public static void premain(String arguments, Instrumentation instrumentation) {
    ChaosDukeyAgent agent = parseArguments(arguments);

    ChaosDukeyInterceptor interceptor =
        new ChaosDukeyInterceptor(agent.percentage, agent.maxDelayMillis);
    new AgentBuilder.Default()
        .type(agent.typeMatcher)
        .transform(
            (builder, type, classLoader, module, protectionDomain) ->
                builder.method(agent.methodMatcher).intercept(MethodDelegation.to(interceptor)))
        .installOn(instrumentation);
  }
}
