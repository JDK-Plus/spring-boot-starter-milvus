package plus.jdk.milvus.toolKit;

import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

public class SetAccessibleAction<T extends AccessibleObject> implements PrivilegedAction<T> {
    private final T obj;

    public SetAccessibleAction(T obj) {
        this.obj = obj;
    }

    @Override
    public T run() {
        obj.setAccessible(true);
        return obj;
    }

}
