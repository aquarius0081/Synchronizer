package com.company;

/**
 * Created by Rinat on 03.06.2017.
 */
public class SyncAction {
  private int id;
  private String name;

  public SyncAction(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
