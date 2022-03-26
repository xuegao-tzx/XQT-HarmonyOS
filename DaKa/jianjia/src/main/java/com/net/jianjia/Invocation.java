package com.net.jianjia;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Invocation.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public final class Invocation {
    /**
     * The Method.
     */
    private final Method method;
    /**
     * The Arguments.
     */
    private final List<?> arguments;

    /**
     * Trusted constructor assumes ownership of {@code arguments}.  @param method the method
     *
     * @param method    the method
     * @param arguments the arguments
     */
    private Invocation(Method method, List<?> arguments) {
        this.method = method;
        this.arguments = Collections.unmodifiableList(arguments);
    }

    /**
     * Of invocation.
     *
     * @param method    the method
     * @param arguments the arguments
     * @return the invocation
     */
    public static Invocation of(Method method, List<?> arguments) {
        Utils.checkNotNull(method, "method == null");
        Utils.checkNotNull(arguments, "arguments == null");
        return new Invocation(method, new ArrayList<>(arguments)); // Defensive copy.
    }

    /**
     * Method method.
     *
     * @return the method
     */
    public Method method() {
        return this.method;
    }

    /**
     * Arguments list.
     *
     * @return the list
     */
    public List<?> arguments() {
        return this.arguments;
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return String.format("%s.%s() %s",
                this.method.getDeclaringClass().getName(), this.method.getName(), this.arguments);
    }
}
