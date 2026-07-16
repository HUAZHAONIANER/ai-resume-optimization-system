package org.example.resumeai.entity;

/**
 * 岗位实体类
 */
public class Job {
    private Integer id;
    private String title;          // 岗位名称
    private String company;        // 公司名称
    private String description;    // 岗位描述
    private String requirements;   // 任职要求
    private String salary;         // 薪资范围
    private String location;       // 工作地点
    private String type;           // 工作类型：全职/实习/兼职
    private String createTime;

    public Job() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", location='" + location + '\'' +
                ", type='" + type + '\'' +
                ", salary='" + salary + '\'' +
                '}';
    }
}
