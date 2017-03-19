package com.company;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;

/**
 * Intermediate class which is used for marshalling to XML file
 */
@XmlRootElement(name = "Jobs")
@XmlAccessorType(XmlAccessType.FIELD)
public class Jobs {
  @XmlElement(name = "Job")
  private HashSet<Job> jobs = null;

  /**
   * Getter for {@code jobs} field
   *
   * @return {@code jobs} field value
   */
  public HashSet<Job> getJobs() {
    return jobs;
  }

  /**
   * Setter for {@code jobs} field
   *
   * @param jobs value to set for {@code jobs} field
   */
  public void setJobs(HashSet<Job> jobs) {
    this.jobs = jobs;
  }
}
