package com.net.jianjia;

import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.system.version.SystemVersion;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.Collections.singletonList;

/**
 * The type Platform.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
class Platform {

    /**
     * The constant PLATFORM.
     */
    private static final Platform PLATFORM = findPlatform();

    /**
     * Get platform.
     *
     * @return the platform
     */
    static Platform get() {
        return PLATFORM;
    }

    /**
     * Find platform platform.
     *
     * @return the platform
     */
    private static Platform findPlatform() {
        try {
            Class.forName("ohos.system.version.SystemVersion");
            if (SystemVersion.getApiVersion() != 0) return new Harmony();
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("java.util.Optional");
            return new Java8();
        } catch (ClassNotFoundException ignored) {
        }
        return new Platform();
    }

    /**
     * Default callback executor executor.
     *
     * @return the executor
     */
    public Executor defaultCallbackExecutor() {
        return null;
    }

    /**
     * Default call adapter factories list.
     *
     * @param callbackExecutor the callback executor
     * @return the list
     */
    List<? extends CallAdapter.Factory> defaultCallAdapterFactories(Executor callbackExecutor) {
        return singletonList(new DefaultCallAdapterFactory(callbackExecutor));
    }

    /**
     * Default converter factories size int.
     *
     * @return the int
     */
    int defaultConverterFactoriesSize() {
        return 1;
    }

    /**
     * Default converter factories list.
     *
     * @return the list
     */
    List<? extends Converter.Factory> defaultConverterFactories() {
        return Collections.emptyList();
    }

    /**
     * Is default method boolean.
     *
     * @param method the method
     * @return the boolean
     */
    boolean isDefaultMethod(Method method) {
        return method.isDefault();
    }

    /**
     * Invoke default method object.
     *
     * @param method         the method
     * @param declaringClass the declaring class
     * @param object         the object
     * @param args           the args
     * @return the object
     * @throws Throwable the throwable
     */
    Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object, Object... args)
            throws Throwable {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(declaringClass, -1 /* trusted */)
                .unreflectSpecial(method, declaringClass)
                .bindTo(object)
                .invokeWithArguments(args);
    }

    /**
     * The type Harmony.
     */
    static class Harmony extends Platform {

        /**
         * Default callback executor executor.
         *
         * @return the executor
         */
        @Override
        public Executor defaultCallbackExecutor() {
            return new MainThreadExecutorOnHarmony();
        }

        /**
         * The type Main thread executor on harmony.
         */
        static class MainThreadExecutorOnHarmony implements Executor {

            /**
             * The Event handler.
             */
            private final EventHandler eventHandler = new EventHandler(EventRunner.getMainEventRunner());

            /**
             * Execute.
             *
             * @param runnable the runnable
             */
            @Override
            public void execute(Runnable runnable) {
                this.eventHandler.postTask(runnable);
            }
        }
    }

    /**
     * The type Java 8.
     */
    private static class Java8 extends Platform {

    }

}
