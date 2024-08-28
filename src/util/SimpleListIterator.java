package util;

import java.util.Iterator;

public class SimpleListIterator<T> implements Iterator<T> {
    private Node<T> current;

    // инициализация итератора
    public SimpleListIterator(Node<T> node) {
        current = node;
    }

    /**
     * Проверка наличия данных в списке
     *
     * @return - возвращает false если данных нет
     */
    @Override
    public boolean hasNext() {
        return current != null;
    }

    /**
     * Чтение очередного элемента
     *
     * @return - элемент данных
     */
    @Override
    public T next() {
        T data = current.getData();
        current = current.getNext();
        return data;
    }

    /**
     * Не поддерживаемая операция - формируем исключение
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

