package com.company;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;

/**
 *
 */
@XmlRootElement(name = "Jobs")
@XmlAccessorType(XmlAccessType.FIELD)
public class Jobs {
    @XmlElement(name = "Job")
    private HashSet<Job> jobs = null;

    public HashSet<Job> getJobs() {
        return jobs;
    }

    public void setJobs(HashSet<Job> jobs) {
        this.jobs = jobs;
    }
}
