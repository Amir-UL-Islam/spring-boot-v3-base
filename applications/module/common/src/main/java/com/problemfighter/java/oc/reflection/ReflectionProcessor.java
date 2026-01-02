package com.problemfighter.java.oc.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class ReflectionProcessor {
    public List<Class<?>> getAllSuperClass(Class<?> klass) {
        List<Class<?>> classes = new ArrayList();
        if (klass != null) {
            for(Class<?> superclass = klass.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
                classes.add(superclass);
            }
        }

        return classes;
    }

    public List<Class<?>> getAllClass(Class<?> klass) {
        if (klass == null) {
            return new ArrayList();
        } else {
            List<Class<?>> classes = this.getAllSuperClass(klass);
            classes.add(klass);
            return classes;
        }
    }

    public List<Field> getAllField(Class<?> klass) {
        List<Field> fields = new ArrayList();
        if (klass != null) {
            for(Class<?> pClass : this.getAllClass(klass)) {
                fields.addAll(Arrays.asList(pClass.getDeclaredFields()));
            }
        }

        return fields;
    }

    private Field getDeclaredField(Object object, String fieldName) {
        return this.getDeclaredField(object.getClass(), fieldName);
    }

    private Field getDeclaredField(Class<?> klass, String fieldName) {
        try {
            return klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException var4) {
            return null;
        }
    }

    private Field getField(Object object, String fieldName) {
        try {
            return object.getClass().getField(fieldName);
        } catch (NoSuchFieldException var4) {
            return null;
        }
    }

    public Field getFieldFromObject(Object object, String fieldName) {
        return this.getFieldFromObject(object.getClass(), fieldName);
    }

    public Field getFieldFromObject(Class<?> klass, String fieldName) {
        Field field = this.getDeclaredField(klass, fieldName);
        if (field == null) {
            field = this.getField(klass, fieldName);
        }

        if (field != null) {
            field.setAccessible(true);
        }

        return field;
    }

    public Field getAnyFieldFromObject(Object object, String fieldName) {
        return this.getAnyFieldFromKlass(object.getClass(), fieldName);
    }

    public Field getAnyFieldFromKlass(Class<?> klass, String fieldName) {
        Field field = this.getFieldFromObject(klass, fieldName);
        if (field == null) {
            for(Class<?> superclass = klass.getSuperclass(); superclass != null; superclass = superclass.getSuperclass()) {
                field = this.getDeclaredField(superclass, fieldName);
                if (field != null) {
                    return field;
                }
            }
        }

        return field;
    }

    public <D> D newInstance(Class<D> klass) {
        try {
            return (D)klass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException var3) {
            return null;
        }
    }

    public Boolean isPrimitive(Class<?> c) {
        return c.isPrimitive() || c == String.class || c == Boolean.class || c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Float.class || c == Double.class || c == BigDecimal.class || c == BigInteger.class || c == LocalDate.class || c == LocalDateTime.class || c == Date.class || c == Timestamp.class || c == Long.class;
    }

    public Boolean isList(Class<?> c) {
        return c == List.class || c == Collection.class || c == LinkedList.class || c == ArrayList.class || c == Vector.class || c == Stack.class;
    }

    public Boolean isSet(Class<?> c) {
        return c == TreeSet.class || c == Set.class || c == LinkedHashSet.class || c == HashSet.class || c == SortedSet.class;
    }

    public Boolean isQueue(Class<?> c) {
        return c == Queue.class || c == PriorityQueue.class || c == Deque.class;
    }

    public Boolean isMap(Class<?> c) {
        return c == Map.class || c == LinkedHashMap.class || c == HashMap.class || c == SortedMap.class || c == TreeMap.class;
    }

    public Collection<?> instanceOfList(Class<?> c) {
        if (c == LinkedList.class) {
            return new LinkedList();
        } else if (c == Vector.class) {
            return new Vector();
        } else {
            return (Collection<?>)(c == Stack.class ? new Stack() : new ArrayList());
        }
    }

    public Queue<?> instanceOfQueue(Class<?> c) {
        return (Queue<?>)(c != ArrayDeque.class && c != Deque.class ? new PriorityQueue() : new ArrayDeque());
    }

    public Set<?> instanceOfSet(Class<?> c) {
        if (c != TreeSet.class && c != SortedSet.class) {
            return (Set<?>)(c == HashSet.class ? new HashSet() : new LinkedHashSet());
        } else {
            return new TreeSet();
        }
    }

    public Map<?, ?> instanceOfMap(Class<?> c) {
        if (c == HashMap.class) {
            return new HashMap();
        } else {
            return (Map<?, ?>)(c != TreeMap.class && c != SortedMap.class ? new LinkedHashMap() : new TreeMap());
        }
    }

    public Method getMethod(Class<?> c, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        return c.getDeclaredMethod(name, parameterTypes);
    }

    public Boolean isMethodExist(Class<?> c, String name, Class<?>... parameterTypes) {
        try {
            this.getMethod(c, name, parameterTypes);
            return true;
        } catch (NoSuchMethodException var5) {
            return false;
        }
    }

    public Object invokeMethod(Object object, String name, Object... parameterTypes) {
        try {
            int paramLength = parameterTypes.length;
            Class<?>[] classes = new Class[paramLength];

            for(int i = 0; i < paramLength; ++i) {
                classes[i] = parameterTypes[i].getClass();
            }

            Method method = this.getMethod(object.getClass(), name, classes);
            if (method != null) {
                return method.invoke(object, parameterTypes);
            }
        } catch (IllegalAccessException | NoSuchMethodException e) {
            ((ReflectiveOperationException)e).printStackTrace();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException)e.getCause();
            }

            if (e.getCause() instanceof Error) {
                throw (Error)e.getCause();
            }
        }

        return null;
    }
}
