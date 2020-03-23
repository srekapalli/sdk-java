/*
 *  Copyright (C) 2020 Temporal Technologies, Inc. All Rights Reserved.
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package io.temporal.internal.testservice;

import io.temporal.proto.common.WorkflowExecutionInfo;
import io.temporal.proto.workflowservice.GetWorkflowExecutionHistoryRequest;
import io.temporal.proto.workflowservice.GetWorkflowExecutionHistoryResponse;
import io.temporal.proto.workflowservice.PollForActivityTaskRequest;
import io.temporal.proto.workflowservice.PollForActivityTaskResponse;
import io.temporal.proto.workflowservice.PollForDecisionTaskRequest;
import io.temporal.proto.workflowservice.PollForDecisionTaskResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

interface TestWorkflowStore {

  enum WorkflowState {
    OPEN,
    CLOSED
  }

  class TaskListId {

    private final String domain;
    private final String taskListName;

    public TaskListId(String domain, String taskListName) {
      this.domain = Objects.requireNonNull(domain);
      this.taskListName = Objects.requireNonNull(taskListName);
    }

    public String getDomain() {
      return domain;
    }

    public String getTaskListName() {
      return taskListName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || !(o instanceof TaskListId)) {
        return false;
      }

      TaskListId that = (TaskListId) o;

      if (!domain.equals(that.domain)) {
        return false;
      }
      return taskListName.equals(that.taskListName);
    }

    @Override
    public int hashCode() {
      int result = domain.hashCode();
      result = 31 * result + taskListName.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "TaskListId{"
          + "domain='"
          + domain
          + '\''
          + ", taskListName='"
          + taskListName
          + '\''
          + '}';
    }
  }

  class DecisionTask {

    private final TaskListId taskListId;
    private final PollForDecisionTaskResponse.Builder task;

    public DecisionTask(TaskListId taskListId, PollForDecisionTaskResponse.Builder task) {
      this.taskListId = taskListId;
      this.task = task;
    }

    public TaskListId getTaskListId() {
      return taskListId;
    }

    public PollForDecisionTaskResponse.Builder getTask() {
      return task;
    }
  }

  class ActivityTask {

    private final TaskListId taskListId;
    private final PollForActivityTaskResponse.Builder task;

    public ActivityTask(TaskListId taskListId, PollForActivityTaskResponse.Builder task) {
      this.taskListId = taskListId;
      this.task = task;
    }

    public TaskListId getTaskListId() {
      return taskListId;
    }

    public PollForActivityTaskResponse.Builder getTask() {
      return task;
    }
  }

  SelfAdvancingTimer getTimer();

  long currentTimeMillis();

  long save(RequestContext requestContext);

  void applyTimersAndLocks(RequestContext ctx);

  void registerDelayedCallback(Duration delay, Runnable r);

  PollForDecisionTaskResponse.Builder pollForDecisionTask(PollForDecisionTaskRequest pollRequest)
      throws InterruptedException;

  PollForActivityTaskResponse.Builder pollForActivityTask(PollForActivityTaskRequest pollRequest)
      throws InterruptedException;

  /** @return queryId */
  void sendQueryTask(
      ExecutionId executionId, TaskListId taskList, PollForDecisionTaskResponse.Builder task);

  GetWorkflowExecutionHistoryResponse getWorkflowExecutionHistory(
      ExecutionId executionId, GetWorkflowExecutionHistoryRequest getRequest);

  void getDiagnostics(StringBuilder result);

  List<WorkflowExecutionInfo> listWorkflows(WorkflowState state, Optional<String> workflowId);

  void close();
}