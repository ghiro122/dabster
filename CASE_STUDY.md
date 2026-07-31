# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
I'd first understand which costs belong directly to a Warehouse or Store, and which ones are shared. For shared costs, we need simple and clear allocation rules.

Questions:
- Which system is the source of cost data?
- How often is the data updated?
- How are shared costs allocated?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
I'd start from the biggest cost areas, like transport, labor, inventory or unused capacity. Then I'd compare possible savings with the risk of reducing service quality.

Questions:
- Where are the biggest costs today?
- Which service levels must stay the same?
- How will we measure the result?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
The integration is important because both systems should use the same numbers. I'd use clear data contracts, stable IDs, retries and checks between both systems.

Questions:
- Which system is the source of truth?
- Which data really needs real-time updates?
- How are failed updates retried?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
I'd use historical costs, expected volume, seasonality and planned changes. The system should also compare budget, forecast and real costs.

Questions:
- How often is the forecast updated?
- Which data is used for the forecast?
- Who approves the budget?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
The old and new Warehouse can share the Business Unit Code, but they should have different IDs. This keeps the old cost history separate from the new Warehouse costs.

Questions:
- Which costs belong to the old Warehouse?
- Which costs belong to the new one?
- Should transition costs be tracked separately?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
