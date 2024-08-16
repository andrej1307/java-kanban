import util.SimpleLinkedList;

public class Main {

    public static void main(String[] args) {

        System.out.println("\nпроверка работы списка");
        SimpleLinkedList<String> sList = new SimpleLinkedList<>();

        sList.addLast("111");
        sList.addLast("222");
        sList.addLast("333");
        sList.addLast("444");
        sList.addLast("555");

        for (String s : sList) {
            System.out.print(s + "; ");
        }

        System.out.println("\nУдаляем первый элемент списка - " + sList.getFirst());
        sList.removeFirst();

        for (String s : sList) {
            System.out.print(s + "; ");
        }

        System.out.println("\nУдаляем концевой элемент списка - " + sList.getLast());
        sList.removeLast();

        for (String s : sList) {
            System.out.print(s + "; ");
        }

        System.out.println("\nОчищаем список");
        sList.clear();

        System.out.println("Размер списка : " + sList.size()
        + "\nГолова : " + sList.getHead() + "\nХвост : " + sList.getTail());
    }

}
