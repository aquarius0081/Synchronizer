package com.company;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Intermediate class which is used for marshalling to XML file
 */
@XmlRootElement(name = "Jobs")
@XmlAccessorType(XmlAccessType.FIELD)
public class Jobs {
  @XmlElement(name = "Job")
  private List<Job> jobs = null;

  /**
   * Getter for {@code jobs} field
   *
   * @return {@code jobs} field value
   */
  public List<Job> getJobs() {
    return jobs;
  }

  /**
   * Setter for {@code jobs} field
   *
   * @param jobs
   *          value to set for {@code jobs} field
   */
  public void setJobs(List<Job> jobs) {
    this.jobs = jobs;
  }
}
