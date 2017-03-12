package com.company;

import javax.xml.bind.annotation.*;

@XmlRootElement
@XmlType(propOrder = {"depcode","depjob","description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Job {
    @XmlTransient
    private Long id;
    private String depcode;
    private String depjob;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepcode() {
        return depcode;
    }

    public void setDepcode(String depcode) {
        this.depcode = depcode;
    }

    public String getDepjob() {
        return depjob;
    }

    public void setDepjob(String depjob) {
        this.depjob = depjob;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
