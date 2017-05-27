package com.company;



import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.*;

/**
 * Intermediate class which is used for marshalling to XML file and as an entity to hold data from DB table row or data
 * for one XML node
 */
@XmlRootElement
@XmlType(propOrder = {"depcode", "depjob", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table( name = "Job" )
public class Job {
  @XmlTransient
  private Long id;
  private String depcode;
  private String depjob;
  private String description;

  public Job(){

  }

  public Job(String depcode, String depjob, String description) {
    this.depcode = depcode;
    this.depjob = depjob;
    this.description = description;
  }

  /**
   * Getter for {@code id} field
   *
   * @return {@code id} field value
   */
  public Long getId() {
    return id;
  }

  /**
   * Setter for {@code id} field
   *
   * @param id value to set for {@code id} field
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Getter for {@code depcode} field
   *
   * @return {@code depcode} field value
   */
  public String getDepcode() {
    return depcode;
  }

  /**
   * Setter for {@code depcode} field
   *
   * @param depcode value to set for {@code depcode} field
   */
  public void setDepcode(String depcode) {
    this.depcode = depcode;
  }

  /**
   * Getter for {@code depjob} field
   *
   * @return {@code depjob} field value
   */
  public String getDepjob() {
    return depjob;
  }

  /**
   * Setter for {@code depjob} field
   *
   * @param depjob value to set for {@code depjob} field
   */
  public void setDepjob(String depjob) {
    this.depjob = depjob;
  }

  /**
   * Getter for {@code description} field
   *
   * @return {@code description} field value
   */
  public String getDescription() {
    return description;
  }

  /**
   * Setter for {@code description} field
   *
   * @param description value to set for {@code description} field
   */
  public void setDescription(String description) {
    this.description = description;
  }
}
