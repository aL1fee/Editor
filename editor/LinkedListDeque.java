package editor;

import java.util.Iterator;
import java.lang.Iterable;

public class LinkedListDeque<Item> implements Iterable<Item> {

    private class LinkedListNode {
        private Item item;
        private LinkedListNode next;
        private LinkedListNode prev;

        public LinkedListNode(Item i, LinkedListNode n, LinkedListNode p) {
            item = i;
            next = n;
            prev = p;
        }
    }

    protected class Itr implements Iterator<Item>  {
        private LinkedListNode current;
        Itr(LinkedListNode sent) {
            current = sent;
        }
        public boolean hasNext() {
            return current.next != null;
        }
        public Item next() {
            Item retItem = current.next.item;
            current = current.next;
            return retItem;
        }
    }

    public Iterator<Item> iterator() {
        return new Itr(sentinel);
    }

    private int size;
    private LinkedListNode currentNode;
    private LinkedListNode sentinel;
    private Itr iterator;

    public LinkedListDeque() {
        size = 0;
        currentNode = new LinkedListNode(null, null, null);
        sentinel = currentNode;
        iterator = new Itr(sentinel);
    }

    public void moveCurrentRight() {
        if (currentNode.next != null) {
            currentNode = currentNode.next;
        }
    }

    public void moveCurrentLeft() {
        if (currentNode.prev != null) {
            currentNode = currentNode.prev;
        }
    }

    public Item getCurrentItem() {
        if (currentNode != null) {
            return currentNode.item;
        }
        return null;
    }

    public Item getPreviousItem() {
        if (currentNode != null) {
            return currentNode.prev.item;
        }
        return null;
    }

    public void clearCursor() {
        currentNode = sentinel;
    }

    public boolean isTheFirstElement() {
        return currentNode.prev == null;
    }

    public boolean isTheLastElement() {
        return currentNode.next == null;
    }

    public void addLast(Item x) {
        LinkedListNode oldPrevNode = currentNode.prev;
        LinkedListNode oldNextNode = currentNode.next;
        if (oldPrevNode == null && size != 0) {
            currentNode.next = new LinkedListNode(x, oldNextNode, currentNode);
            oldNextNode.prev = currentNode.next;
            currentNode = currentNode.next;
            size++;
            return;
        }
        if (oldPrevNode == null && size == 0) {
            currentNode.next = new LinkedListNode(x, null, currentNode);
            currentNode = currentNode.next;
            size++;
            return;
        }
        if (isTheLastElement()) {
            currentNode.next = new LinkedListNode(x, null, currentNode);
            currentNode = currentNode.next;
            size++;
            return;
        }
        currentNode.next = new LinkedListNode(x, oldNextNode, currentNode);
        oldNextNode.prev = currentNode.next;
        currentNode = currentNode.next;
        size++;
    }

    public Item removeLast() {
        if (size > 0) {
            if (currentNode.item == null) {
                return null;
            }
            LinkedListNode oldPrevNode = currentNode.prev;
            LinkedListNode oldNextNode = currentNode.next;
            Item deletedItem = currentNode.item;
            if (oldNextNode == null) {
                currentNode = oldPrevNode;
                currentNode.next = null;
                size--;
                return deletedItem;
            }
            oldPrevNode.next = oldNextNode;
            oldNextNode.prev = oldPrevNode;
            currentNode = oldPrevNode;
            size--;
            return deletedItem;
        }
        return null;
    }

    public void remove(Item item) {
        iterator = new Itr(sentinel);
        while (iterator.hasNext()) {
            if (iterator.next() == item) {
                LinkedListNode itemNode = iterator.current;
                LinkedListNode oldItemNodePrev = itemNode.prev;
                LinkedListNode oldItemNodeNext = itemNode.next;
                if (oldItemNodeNext == null) {
                    currentNode = oldItemNodePrev;
                    currentNode.next = null;
                    size--;
                    return;
                }
                oldItemNodePrev.next = oldItemNodeNext;
                oldItemNodeNext.prev = oldItemNodePrev;
                currentNode = oldItemNodePrev;
                size--;
                return;
            }
        }
    }

    public void setCurrentNodeAfter(Item item) {
        iterator = new Itr(sentinel);
        while (iterator.hasNext()) {
            if (iterator.next() == item) {
                currentNode = iterator.current.prev;
                break;
            }
        }
    }

    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }

    public int size() {
        return size;
    }
}