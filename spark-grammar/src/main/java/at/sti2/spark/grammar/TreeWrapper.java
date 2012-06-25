package at.sti2.spark.grammar;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.antlr.runtime.tree.Tree;

public class TreeWrapper implements Iterable<TreeWrapper>,Iterator<TreeWrapper>{
	
	private final Tree tree;
	private int size = 0;
	private int cursor;
	
	public TreeWrapper(final Tree tree) {
		this.tree = tree;
		size = tree.getChildCount();
	}

	@Override
	public boolean hasNext() {
		return cursor != size;
	}

	@Override
	public TreeWrapper next() {
		int i = cursor;
		if(i >= size)
			throw new NoSuchElementException();
	cursor = i + 1;
	Tree child = tree.getChild(i);
	return new TreeWrapper(child);
	}

	@Override
	public void remove() {
		
	}

	@Override
	public Iterator<TreeWrapper> iterator() {
		return this;
	}
	
	@Override
	public String toString() {
		return tree.toString();
	}
	
	public int getSize() {
		return size;
	}
	
	public TreeWrapper getChild(int index){
		Tree child = tree.getChild(index);
		return new TreeWrapper(child);
	}
	
}