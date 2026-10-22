package util;

// UNIT II - Generic class.
// <T> means "any type". Holder<Student> holds a Student, Holder<String> holds a String.
// We use it in MainFrame to hold the logged-in student.
public class Holder<T> {

    private T item;

    public void set(T item) {
        this.item = item;
    }

    public T get() {
        return item;
    }

    public boolean isEmpty() {
        return item == null;
    }
}
