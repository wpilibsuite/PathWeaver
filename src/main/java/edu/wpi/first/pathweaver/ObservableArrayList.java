package edu.wpi.first.pathweaver;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ModifiableObservableListBase;

public class ObservableArrayList<T> extends ModifiableObservableListBase<T> {
  private final List<T> backingList;

  public ObservableArrayList(int size) {
    super();
    backingList = new ArrayList<>(size);
  }

  public ObservableArrayList() {
    super();
    backingList = new ArrayList<>();
  }

  @Override
  public T get(int i) {
    return backingList.get(i);
  }

  @Override
  public int size() {
    return backingList.size();
  }

  @Override
  protected void doAdd(int i, T o) {
    backingList.add(i, o);
  }

  @Override
  protected T doSet(int i, T o) {
    return backingList.set(i, o);
  }

  @Override
  protected T doRemove(int i) {
    return backingList.remove(i);
  }
}
