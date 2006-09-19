package edu.mit.simile.crowbar.tests;

import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;
import org.mozilla.xpcom.nsIComponentManager;
import org.mozilla.xpcom.nsIMutableArray;
import org.mozilla.xpcom.nsISupports;

public class ArrayTest extends JavaXPCOMTest {

    public static final String NS_ARRAY_CONTRACTID = "@mozilla.org/array;1";

    public void testArray() throws Exception {
        Mozilla mozilla = initMozilla();

        nsIComponentManager componentManager = mozilla.getComponentManager();
        nsIMutableArray array = (nsIMutableArray) componentManager.createInstanceByContractID(NS_ARRAY_CONTRACTID,
                null, nsIMutableArray.NS_IMUTABLEARRAY_IID);
        if (array == null) {
            throw new RuntimeException("Failed to create nsIMutableArray.");
        }

        fillArray(array, 10);
        System.out.println("Array created:");
        int fillResult[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        dumpArray(array, 10, fillResult, 10);

        // test insert
        Foo foo = (Foo) array.queryElementAt(3, nsISupports.NS_ISUPPORTS_IID);
        array.insertElementAt(foo, 5, false);
        System.out.println("insert 3 at 5:");
        int insertResult[] = { 0, 1, 2, 3, 4, 3, 5, 6, 7, 8, 9 };
        dumpArray(array, 11, insertResult, 10);
        System.out.println("insert 3 at 0:");
        array.insertElementAt(foo, 0, false);
        int insertResult2[] = { 3, 0, 1, 2, 3, 4, 3, 5, 6, 7, 8, 9 };
        dumpArray(array, 12, insertResult2, 10);
        System.out.println("append 3:");
        array.appendElement(foo, false);
        int appendResult[] = { 3, 0, 1, 2, 3, 4, 3, 5, 6, 7, 8, 9, 3 };
        dumpArray(array, 13, appendResult, 10);

        // test IndexOf && LastIndexOf
        int expectedIndex[] = { 0, 4, 6, 12, -1 };
        int count = 0;
        long index = array.indexOf(0, foo);
        System.out.println("IndexOf(foo): " + index + "=" + expectedIndex[count] + " "
                + assertEqual(index, expectedIndex[count]));
        try {
            do {
                count++;
                index = array.indexOf(index + 1, foo);
                System.out.println("IndexOf(foo): " + index + "=" + expectedIndex[count] + " "
                        + assertEqual(index, expectedIndex[count]));
            } while (true);
        } catch (XPCOMException e) {
            // If array.indexOf() did not find the element, it returns
            // NS_ERROR_FAILURE.
            if (e.errorcode != Mozilla.NS_ERROR_FAILURE) {
                throw e;
            }
        }

        index = lastIndexOf(array, foo);
        count--;
        System.out.println("LastIndexOf(foo): " + index + "=" + expectedIndex[count] + " "
                + assertEqual(index, expectedIndex[count]));

        System.out.println("ReplaceElementAt(8):");
        replaceElementAt(array, foo, 8);
        System.gc();
        int replaceResult[] = { 3, 0, 1, 2, 3, 4, 3, 5, 3, 7, 8, 9, 3 };
        dumpArray(array, 13, replaceResult, 9);

        System.out.println("RemoveElementAt(0):");
        array.removeElementAt(0);
        System.gc();
        int removeResult[] = { 0, 1, 2, 3, 4, 3, 5, 3, 7, 8, 9, 3 };
        dumpArray(array, 12, removeResult, 9);
        System.out.println("RemoveElementAt(7):");
        array.removeElementAt(7);
        System.gc();
        int removeResult2[] = { 0, 1, 2, 3, 4, 3, 5, 7, 8, 9, 3 };
        dumpArray(array, 11, removeResult2, 9);
        System.out.println("RemoveElement(foo):");
        removeElement(array, foo);
        System.gc();
        int removeResult3[] = { 0, 1, 2, 4, 3, 5, 7, 8, 9, 3 };
        dumpArray(array, 10, removeResult3, 9);
        System.out.println("RemoveLastElement(foo):");
        removeLastElement(array, foo);
        System.gc();
        int removeResult4[] = { 0, 1, 2, 4, 3, 5, 7, 8, 9 };
        dumpArray(array, 9, removeResult4, 9);

        // test clear
        foo = null; // remove ref now, so everything is cleared
        System.out.println("clear array:");
        array.clear();
        System.gc();
        dumpArray(array, 0, null, 0);
        System.out.println("add 4 new:");
        fillArray(array, 4);
        dumpArray(array, 4, fillResult, 4);

        // test deleting of array
        System.out.println("release array:");
        array = null;
        System.gc();
        dumpArray(array, 0, null, 0);

        componentManager = null;
        System.gc();
        shutdownMozilla(mozilla);

        System.out.println("Test Passed.");
    }

    static void fillArray(nsIMutableArray aArray, int aCount) {
        for (int index = 0; index < aCount; index++) {
            nsISupports foo = new Foo(index);
            aArray.appendElement(foo, false);
        }
    }

    static String assertEqual(long aValue1, long aValue2) {
        if (aValue1 == aValue2) return "OK";
        return "ERROR";
    }

    static void dumpArray(nsIMutableArray aArray, int aExpectedCount, int[] aElementIDs, int aExpectedTotal) {
        long count = 0;
        if (aArray != null) count = aArray.getLength();

        System.out.println("object count " + Foo.gCount + " = " + aExpectedTotal + " "
                + assertEqual(Foo.gCount, aExpectedTotal));
        System.out.println("array count " + count + " = " + aExpectedCount + " " + assertEqual(count, aExpectedCount));

        for (int index = 0; (index < count) && (index < aExpectedCount); index++) {
            Foo foo = (Foo) aArray.queryElementAt(index, nsISupports.NS_ISUPPORTS_IID);
            System.out.println(index + ": " + aElementIDs[index] + "=" + foo.getId() + " ("
                    + Integer.toHexString(foo.hashCode()) + ") " + assertEqual(foo.getId(), aElementIDs[index]));
            foo = null;
        }
    }

    static long lastIndexOf(nsIMutableArray aArray, nsISupports aElement) {
        for (long i = aArray.getLength() - 1; i >= 0; i--) {
            Foo foo = (Foo) aArray.queryElementAt(i, nsISupports.NS_ISUPPORTS_IID);
            if (foo == aElement) return i;
        }
        return -1;
    }

    static void replaceElementAt(nsIMutableArray aArray, nsISupports aElement, int aIndex) {
        aArray.removeElementAt(aIndex);
        aArray.insertElementAt(aElement, aIndex, false);
    }

    /* Removes first instance of given element */
    static void removeElement(nsIMutableArray aArray, nsISupports aElement) {
        long index = aArray.indexOf(0, aElement);
        aArray.removeElementAt(index);
    }

    static void removeLastElement(nsIMutableArray aArray, nsISupports aElement) {
        long index = lastIndexOf(aArray, aElement);
        aArray.removeElementAt(index);
    }
}
