package com.example.servicetest.dto;

import java.io.Serializable;

public class TestNotificationDTO implements Serializable {
    private Long testId;
    private String testTitle;
    private String status;
    private Long userId;
    private String userName;

    public TestNotificationDTO() {
    }

    public TestNotificationDTO(Long testId, String testTitle, String status, Long userId, String userName) {
        this.testId = testId;
        this.testTitle = testTitle;
        this.status = status;
        this.userId = userId;
        this.userName = userName;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "TestNotificationDTO{" +
                "testId=" + testId +
                ", testTitle='" + testTitle + '\'' +
                ", status='" + status + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
