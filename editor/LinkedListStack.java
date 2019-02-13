package editor;

import java.util.Iterator;
import java.lang.Iterable;

public class LinkedListStack<Item> implements Iterable<Item> {

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

    private class Itr implements Iterator<Item>  {
        private LinkedListNode current;
        Itr(LinkedListNode sent) {
            current = sent;
        }
        public boolean hasNext() {
            return current.next.item != null;
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
    private int maxSize = 100;
    private LinkedListNode currentNode;
    private LinkedListNode sentinel;

    public LinkedListStack() {
        size = 0;
        sentinel = new LinkedListNode(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        currentNode = sentinel;
        Itr iterator = new Itr(currentNode);
    }

    public void push(Item x) {
        if (size < maxSize) {
            LinkedListNode oldPrevNode = sentinel.prev;
            if (oldPrevNode.item == null) {
                sentinel.next = new LinkedListNode(x, sentinel, oldPrevNode);
                sentinel.prev = sentinel.next;
                size++;
                return;
            }
            sentinel.prev = new LinkedListNode(x, sentinel, oldPrevNode);
            oldPrevNode.next = sentinel.prev;
            currentNode = sentinel.prev;
            size++;
        } else if (size == maxSize) {
            if (currentNode.next == sentinel) {
                currentNode = currentNode.next.next;
                currentNode.item = x;
            } else {
                currentNode = currentNode.next;
                currentNode.item = x;
            }
        }
    }

    public Item pop() {
        if (size > 0 && size < maxSize) {
            Item deletedItem;
            if (currentNode == sentinel) {
                currentNode = sentinel.prev;
                deletedItem = currentNode.item;
            }
            deletedItem = currentNode.item;
            LinkedListNode oldNextNode = currentNode.next;
            currentNode = currentNode.prev;
            currentNode.next = oldNextNode;
            oldNextNode.prev = currentNode;
            size--;
            return deletedItem;
        } else if (size == maxSize) {
            LinkedListNode oldNextNode = currentNode.next;
            Item deletedItem = currentNode.prev.item;
            currentNode = currentNode.prev;
            currentNode.next = oldNextNode;
            oldNextNode.prev = currentNode;
            size--;
            return deletedItem;
        }
        return null;
    }

    public Item peek() {
        return sentinel.prev.item;
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