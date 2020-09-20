package Utility;

import java.util.LinkedList;

public class Printing {

	public static void dumpContents(Object root) {
		LinkedList<Object> elements = new LinkedList<>();
		elements.add(root);

		while (!elements.isEmpty()) {
			Object par = elements.removeLast();
			System.out.println(par.toString());

			if (par instanceof Iterable) {
				for (Object o : (Iterable<?>) par) {
					enqueuePrintDumpElement(o, elements);
				}
			} else if (par.getClass().isArray()) {
				for (Object o : (Object[]) par) {
					enqueuePrintDumpElement(o, elements);
				}
			}
		}
	}

	private static void enqueuePrintDumpElement(Object o, LinkedList<Object> elements) {
		if (o == null) {
			System.out.println("null");
			return;
		}

		if (o instanceof Iterable || o.getClass().isArray()) {
			elements.addLast(o);
		}
	}
}
