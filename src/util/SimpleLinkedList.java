package util;
import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * Класс реализации двунаправленного связанного списка
 * @param <T> - тип элементов списка
 */
public class SimpleLinkedList<T> implements Iterable<T> {
    // Указатель на первый элемент списка. Он же first
    private Node<T> head;

    // Указатель на последний элемент списка. Он же last
    private Node<T> tail;

    private int size = 0;

    /**
     * Добавление нового элемента в начало списка
     * @param element - новый элемент
     */
    public void addFirst(T element) {
        final Node<T> oldHead = head;
        final Node<T> newNode = new Node<>(null, element, oldHead);
        head = newNode;
        if (oldHead == null) {
            tail = newNode;
        }
        else {
            oldHead.setPrev(newNode);
        }
        size++;
    }

    /**
     * чтение первого элемента списка
     * @return - первый элемент списка
     */
    public T getFirst() {
        final Node<T> curHead = head;
        if (curHead == null)
            throw new NoSuchElementException();
        return head.getData();
    }

    /**
     * добавление элемента в конец списка
     * @param element - новый элемент
     */
    public void addLast(T element) {
        final Node<T> oldTail = tail;
        final Node<T> newNode = new Node<>(oldTail, element, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.setNext(newNode);
        }
        size++;
    }

    /**
     * Чтение последнего элемента списка
     * @return - последний элеент
     */
    public T getLast() {
        // Реализуйте метод
        final Node<T> curTail = tail;
        if (curTail == null)
            throw new NoSuchElementException();
        return tail.getData();
    }

    /**
     * Чтение размера списка
     * @return - число элементов в списке
     */
    public int size() {
        return this.size;
    }

    /**
     * Получение ссылки на начало списка
     * @return - ссылка на первый узел связанного списка
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * Получение ссылки на конец списка
     * @return - ссылка на конечный  узел связанного списка
     */
    public Node<T> getTail() {
        return tail;
    }

    /**
     * Создание итератора для перебора элементов списка
     * @return - итератор
     */
    public Iterator<T> iterator() {
        return new SimpleListIterator<>(this);
    }

    /**
     * Удаление узла из связанного списка
     * @param node - узел для удаления
     */
    public void removeNode(Node<T> node) {
        if (node == null || size == 0) return;

        final Node<T> prevNode = node.getPrev();
        final Node<T> nextNode = node.getNext();

        if (prevNode != null) {
            prevNode.setNext(node.getNext());
        } else {
            head = nextNode;
        }

        if (nextNode != null) {
            nextNode.setPrev(node.getPrev());
        } else {
            tail = prevNode;
        }

        node.toFree();
        size--;
    }

    /**
     * Добавление в конец списка подготовленного узла
     * @param node - узел для добавления
     */
    public void addLastNode(Node<T> node) {
        final Node<T> oldTail = tail;
        tail = node;
        tail.setPrev(oldTail);
        if (oldTail == null) {
            head = node;
        } else {
            oldTail.setNext(node);
        }
        size++;
    }

    public void removeFirst(){
        removeNode(head);
    }

    public void removeLast(){
        removeNode(tail);
    }

    /**
     * очистка содержимого списка
     */
    public void clear() {
        while (size != 0) {
            removeNode(tail);
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

}
