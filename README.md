# digital_driving_log
A project to digitalize driving logs (for private use).

## Whole project
All parts for this project will be linkes in this part when available.

## Concept
Goal of this project is to digitalize driving logs for tracking driving use of cars which are shared between multiple persons.
First stage often is a manual driving log with maybe an excel table for calculating the cost share of each individual.
With this software the participants of a shared car should be able to communicate to a **localy** hosted server (intranet) and upload their driving data.

## Vision
The following use cases should be implemented:
1. A driver can enter the driven distance into a form
2. The form can be enhanced when gas was refilled (Volume, Cost_sum)
3. If a purchase was made it can be entered in a form with the options of variable or fix costs
4. Fix costs (insurance) will be distributed equally on the shareholders
5. Variable costs (gas, weare parts like wheels) will be distributed per driven distance
6. The entered values will be synced up with the server when in the same local network (project can be upgraded with authentication and authorization for accessing it from the internet per IP/URI)
7. The server provides an overview of the current cost distribution
8. The distribution will be automatically closed when the year ends and beginns new for the next year (I may put a switch to disable this feature and only work with the following)
9. The distribution can add and remove shareholders (an option may be implemented that temporary participants only have to pay variable costs)
