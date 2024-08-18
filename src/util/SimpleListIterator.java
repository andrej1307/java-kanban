package util;

import java.util.Iterator;

public class SimpleListIterator<T> implements Iterator<T> {
    Node<T> current;

    // инициализация итератора
    public SimpleListIterator(SimpleLinkedList<T> list) {
        current = list.getHead();
    }

    /**
     * Проверка наличия данных в списке
     *
     * @return - возвращает false если данных нет
     */
    public boolean hasNext() {
        return current != null;
    }

    /**
     * Чтение очередного элемента
     *
     * @return - элемент данных
     */
    public T next() {
        T data = current.getData();
        current = current.getNext();
        return data;
    }

    /**
     * Не поддерживаемая операция - формируем исключение
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

