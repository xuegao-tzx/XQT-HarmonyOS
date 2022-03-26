package com.net.jianjia;

import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * The type Utils.
 *
 * @modify&fix 田梓萱
 * @date 2022 /2/17
 */
public class Utils {

    /**
     * The Empty type array.
     */
    static final Type[] EMPTY_TYPE_ARRAY = new Type[0];

    /**
     * Check not null t.
     *
     * @param <T>     the type parameter
     * @param object  the object
     * @param message the message
     * @return the t
     */
    static <T> T checkNotNull(T object, String message) {
        if (object == null) throw new NullPointerException(message);
        return object;
    }

    /**
     * Check not primitive.
     *
     * @param type the type
     */
    static void checkNotPrimitive(Type type) {
        if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) throw new IllegalArgumentException();
    }

    /**
     * Validate service interface.
     *
     * @param <T>     the type parameter
     * @param service the service
     */
    static <T> void validateServiceInterface(Class<T> service) {
        // 必须是接口，Java内置的动态代理只能代理接口
        if (!service.isInterface()) throw new IllegalArgumentException("API declarations must be interfaces.");
        // 接口不能继承其它接口
        if (service.getInterfaces().length > 0)
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
    }

    /**
     * Method error runtime exception.
     *
     * @param method  the method
     * @param message the message
     * @param args    the args
     * @return the runtime exception
     */
    static RuntimeException methodError(Method method, String message, Object... args) {
        return methodError(method, null, message, args);
    }

    /**
     * Method error runtime exception.
     *
     * @param method  the method
     * @param cause   the cause
     * @param message the message
     * @param args    the args
     * @return the runtime exception
     */
    static RuntimeException methodError(Method method, Throwable cause, String message,
                                        Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException(message
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName(), cause);
    }

    /**
     * Has unresolvable type boolean.
     *
     * @param type the type
     * @return the boolean
     */
    static boolean hasUnresolvableType(Type type) {
        if (type instanceof Class<?>) return false;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type typeArgument : parameterizedType.getActualTypeArguments())
                if (hasUnresolvableType(typeArgument)) return true;
            return false;
        }
        if (type instanceof GenericArrayType)
            return hasUnresolvableType(((GenericArrayType) type).getGenericComponentType());
        if (type instanceof TypeVariable) return true;
        if (type instanceof WildcardType) return true;
        String className = type == null ? "null" : type.getClass().getName();
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + className);
    }

    /**
     * Parameter error runtime exception.
     *
     * @param method  the method
     * @param cause   the cause
     * @param p       the p
     * @param message the message
     * @param args    the args
     * @return the runtime exception
     */
    static RuntimeException parameterError(Method method,
                                           Throwable cause, int p, String message, Object... args) {
        return methodError(method, cause, message + " (parameter #" + (p + 1) + ")", args);
    }

    /**
     * Parameter error runtime exception.
     *
     * @param method  the method
     * @param p       the p
     * @param message the message
     * @param args    the args
     * @return the runtime exception
     */
    static RuntimeException parameterError(Method method, int p, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (p + 1) + ")", args);
    }

    /**
     * Gets raw type.
     *
     * @param type the type
     * @return the raw type
     */
    static Class<?> getRawType(Type type) {
        checkNotNull(type, "type == null");

        // Type is a normal class.
        if (type instanceof Class<?>) return (Class<?>) type;
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
        // type that's more general than necessary is okay.
        if (type instanceof TypeVariable) return Object.class;
        if (type instanceof WildcardType) return getRawType(((WildcardType) type).getUpperBounds()[0]);

        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    /**
     * Gets parameter upper bound.
     *
     * @param index the index
     * @param type  the type
     * @return the parameter upper bound
     */
    static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) throw new IllegalArgumentException(
                "Index " + index + " not in range [0," + types.length + ") for " + type);
        Type paramType = types[index];
        if (paramType instanceof WildcardType) return ((WildcardType) paramType).getUpperBounds()[0];
        return paramType;
    }

    /**
     * Equals boolean.
     *
     * @param a the a
     * @param b the b
     * @return the boolean
     */
    static boolean equals(Type a, Type b) {
        if (a == b) return true; // Also handles (a == null && b == null).
        else if (a instanceof Class) return a.equals(b); // Class already specifies equals().
        else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) return false;
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            Object ownerA = pa.getOwnerType();
            Object ownerB = pb.getOwnerType();
            return (ownerA == ownerB || (ownerA != null && ownerA.equals(ownerB)))
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) return false;
            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) return false;
            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) return false;
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());

        } else return false; // This isn't a type we support!
    }

    /**
     * Is annotation present boolean.
     *
     * @param annotations the annotations
     * @param cls         the cls
     * @return the boolean
     */
    static boolean isAnnotationPresent(Annotation[] annotations,
                                       Class<? extends Annotation> cls) {
        for (Annotation annotation : annotations) if (cls.isInstance(annotation)) return true;
        return false;
    }

    /**
     * Buffer response body.
     *
     * @param body the body
     * @return the response body
     * @throws IOException the io exception
     */
    static ResponseBody buffer(ResponseBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.source().readAll(buffer);
        return ResponseBody.create(body.contentType(), body.contentLength(), buffer);
    }

    /**
     * Type to string string.
     *
     * @param type the type
     * @return the string
     */
    static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    /**
     * Resolve type variable type.
     *
     * @param context        the context
     * @param contextRawType the context raw type
     * @param unknown        the unknown
     * @return the type
     */
    private static Type resolveTypeVariable(
            Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        // We can't reduce this further.
        if (declaredByRaw == null) return unknown;

        Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
        if (declaredBy instanceof ParameterizedType) {
            int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
            return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
        }

        return unknown;
    }

    /**
     * Index of int.
     *
     * @param array  the array
     * @param toFind the to find
     * @return the int
     */
    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) if (toFind.equals(array[i])) return i;
        throw new NoSuchElementException();
    }

    /**
     * Declaring class of class.
     *
     * @param typeVariable the type variable
     * @return the class
     */
    private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    /**
     * Gets supertype.
     *
     * @param context        the context
     * @param contextRawType the context raw type
     * @param supertype      the supertype
     * @return the supertype
     */
    static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        if (!supertype.isAssignableFrom(contextRawType)) throw new IllegalArgumentException();
        return resolve(context, contextRawType,
                getGenericSupertype(context, contextRawType, supertype));
    }

    /**
     * Resolve type.
     *
     * @param context        the context
     * @param contextRawType the context raw type
     * @param toResolve      the to resolve
     * @return the type
     */
    private static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        // This implementation is made a little more complicated in an attempt to avoid object-creation.
        while (true) if (toResolve instanceof TypeVariable) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) toResolve;
            toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
            if (toResolve == typeVariable) return toResolve;

        } else if (toResolve instanceof Class && ((Class<?>) toResolve).isArray()) {
            Class<?> original = (Class<?>) toResolve;
            Type componentType = original.getComponentType();
            Type newComponentType = resolve(context, contextRawType, componentType);
            return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                    newComponentType);

        } else if (toResolve instanceof GenericArrayType) {
            GenericArrayType original = (GenericArrayType) toResolve;
            Type componentType = original.getGenericComponentType();
            Type newComponentType = resolve(context, contextRawType, componentType);
            return componentType == newComponentType ? original : new GenericArrayTypeImpl(
                    newComponentType);

        } else if (toResolve instanceof ParameterizedType) {
            ParameterizedType original = (ParameterizedType) toResolve;
            Type ownerType = original.getOwnerType();
            Type newOwnerType = resolve(context, contextRawType, ownerType);
            boolean changed = newOwnerType != ownerType;

            Type[] args = original.getActualTypeArguments();
            for (int t = 0, length = args.length; t < length; t++) {
                Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
                if (resolvedTypeArgument != args[t]) {
                    if (!changed) {
                        args = args.clone();
                        changed = true;
                    }
                    args[t] = resolvedTypeArgument;
                }
            }

            return changed
                    ? new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args)
                    : original;

        } else if (toResolve instanceof WildcardType) {
            WildcardType original = (WildcardType) toResolve;
            Type[] originalLowerBound = original.getLowerBounds();
            Type[] originalUpperBound = original.getUpperBounds();

            if (originalLowerBound.length == 1) {
                Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
                if (lowerBound != originalLowerBound[0])
                    return new WildcardTypeImpl(new Type[]{Object.class}, new Type[]{lowerBound});
            } else if (originalUpperBound.length == 1) {
                Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
                if (upperBound != originalUpperBound[0])
                    return new WildcardTypeImpl(new Type[]{upperBound}, EMPTY_TYPE_ARRAY);
            }
            return original;

        } else return toResolve;
    }

    /**
     * Gets generic supertype.
     *
     * @param context   the context
     * @param rawType   the raw type
     * @param toResolve the to resolve
     * @return the generic supertype
     */
    private static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) return context;

        // We skip searching through interfaces if unknown is an interface.
        if (toResolve.isInterface()) {
            Class<?>[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++)
                if (interfaces[i] == toResolve) return rawType.getGenericInterfaces()[i];
                else if (toResolve.isAssignableFrom(interfaces[i]))
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
        }

        // Check our supertypes.
        if (!rawType.isInterface()) while (rawType != Object.class) {
            Class<?> rawSupertype = rawType.getSuperclass();
            if (rawSupertype == toResolve) return rawType.getGenericSuperclass();
            else if (toResolve.isAssignableFrom(rawSupertype))
                return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
            rawType = rawSupertype;
        }

        // We can't resolve this further.
        return toResolve;
    }

    /**
     * Throw if fatal.
     *
     * @param t the t
     */
    static void throwIfFatal(Throwable t) {
        if (t instanceof VirtualMachineError) throw (VirtualMachineError) t;
        else if (t instanceof ThreadDeath) throw (ThreadDeath) t;
        else if (t instanceof LinkageError) throw (LinkageError) t;
    }

    /**
     * The type Parameterized type.
     */
    static final class ParameterizedTypeImpl implements ParameterizedType {
        /**
         * The Owner type.
         */
        private final Type ownerType;
        /**
         * The Raw type.
         */
        private final Type rawType;
        /**
         * The Type arguments.
         */
        private final Type[] typeArguments;

        /**
         * Instantiates a new Parameterized type.
         *
         * @param ownerType     the owner type
         * @param rawType       the raw type
         * @param typeArguments the type arguments
         */
        ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            // Require an owner type if the raw type needs it.
            if (rawType instanceof Class<?>
                    && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null))
                throw new IllegalArgumentException();

            for (Type typeArgument : typeArguments) {
                Utils.checkNotNull(typeArgument, "typeArgument == null");
                Utils.checkNotPrimitive(typeArgument);
            }

            this.ownerType = ownerType;
            this.rawType = rawType;
            this.typeArguments = typeArguments.clone();
        }

        /**
         * Get actual type arguments type [ ].
         *
         * @return the type [ ]
         */
        @Override
        public Type[] getActualTypeArguments() {
            return this.typeArguments.clone();
        }

        /**
         * Gets raw type.
         *
         * @return the raw type
         */
        @Override
        public Type getRawType() {
            return this.rawType;
        }

        /**
         * Gets owner type.
         *
         * @return the owner type
         */
        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        /**
         * Equals boolean.
         *
         * @param other the other
         * @return the boolean
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && Utils.equals(this, (ParameterizedType) other);
        }

        /**
         * Hash code int.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return Arrays.hashCode(this.typeArguments)
                    ^ this.rawType.hashCode()
                    ^ (this.ownerType != null ? this.ownerType.hashCode() : 0);
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (this.typeArguments.length == 0) return Utils.typeToString(this.rawType);
            StringBuilder result = new StringBuilder(30 * (this.typeArguments.length + 1));
            result.append(Utils.typeToString(this.rawType));
            result.append("<").append(Utils.typeToString(this.typeArguments[0]));
            for (int i = 1; i < this.typeArguments.length; i++)
                result.append(", ").append(Utils.typeToString(this.typeArguments[i]));
            return result.append(">").toString();
        }
    }

    /**
     * The type Generic array type.
     */
    private static final class GenericArrayTypeImpl implements GenericArrayType {
        /**
         * The Component type.
         */
        private final Type componentType;

        /**
         * Instantiates a new Generic array type.
         *
         * @param componentType the component type
         */
        GenericArrayTypeImpl(Type componentType) {
            this.componentType = componentType;
        }

        /**
         * Gets generic component type.
         *
         * @return the generic component type
         */
        @Override
        public Type getGenericComponentType() {
            return this.componentType;
        }

        /**
         * Equals boolean.
         *
         * @param o the o
         * @return the boolean
         */
        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType
                    && Utils.equals(this, (GenericArrayType) o);
        }

        /**
         * Hash code int.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            return this.componentType.hashCode();
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            return Utils.typeToString(this.componentType) + "[]";
        }
    }

    /**
     * The type Wildcard type.
     */
    private static final class WildcardTypeImpl implements WildcardType {
        /**
         * The Upper bound.
         */
        private final Type upperBound;
        /**
         * The Lower bound.
         */
        private final Type lowerBound;

        /**
         * Instantiates a new Wildcard type.
         *
         * @param upperBounds the upper bounds
         * @param lowerBounds the lower bounds
         */
        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            if (lowerBounds.length > 1) throw new IllegalArgumentException();
            if (upperBounds.length != 1) throw new IllegalArgumentException();

            if (lowerBounds.length == 1) {
                if (lowerBounds[0] == null) throw new NullPointerException();
                Utils.checkNotPrimitive(lowerBounds[0]);
                if (upperBounds[0] != Object.class) throw new IllegalArgumentException();
                this.lowerBound = lowerBounds[0];
                this.upperBound = Object.class;
            } else {
                if (upperBounds[0] == null) throw new NullPointerException();
                Utils.checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = upperBounds[0];
            }
        }

        /**
         * Get upper bounds type [ ].
         *
         * @return the type [ ]
         */
        @Override
        public Type[] getUpperBounds() {
            return new Type[]{this.upperBound};
        }

        /**
         * Get lower bounds type [ ].
         *
         * @return the type [ ]
         */
        @Override
        public Type[] getLowerBounds() {
            return this.lowerBound != null ? new Type[]{this.lowerBound} : Utils.EMPTY_TYPE_ARRAY;
        }

        /**
         * Equals boolean.
         *
         * @param other the other
         * @return the boolean
         */
        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && Utils.equals(this, (WildcardType) other);
        }

        /**
         * Hash code int.
         *
         * @return the int
         */
        @Override
        public int hashCode() {
            // This equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds()).
            return (this.lowerBound != null ? 31 + this.lowerBound.hashCode() : 1) ^ (31 + this.upperBound.hashCode());
        }

        /**
         * To string string.
         *
         * @return the string
         */
        @Override
        public String toString() {
            if (this.lowerBound != null) return "? super " + Utils.typeToString(this.lowerBound);
            if (this.upperBound == Object.class) return "?";
            return "? extends " + Utils.typeToString(this.upperBound);
        }
    }
}
