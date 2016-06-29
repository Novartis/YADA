# Security Guide

YADA Security is comprised of a four-tier cascading system featuring

1. URL pattern matching
2. Token authentication
3. Query execution protection
4. Content protection

The mechanism for a YADA Security implementation is a *preprocessor plugin*, configured as a *default YADA parameter* attached to the query for which protection is desired. Such a plugin, `com.novartis.opensource.yada.plugin.Gatekeeper` is provided.

## Notes (to be edited into this document)
* The default parameter implementation limits the request syntax, i.e., execution.policy.columns is a space-delim list of indices or col names, but it needs to support both, so the user isn't constrained. Although, this might be achievable more easily by requiring admins to set up specs with both, and test for request spec, i.e., if the request uses json params use that spec, else use the other, if only one spec exists and the wrong one is passed, throw exception. Currently the solution to this problem is to require 2 spec records, one with each syntax passed in the value of `execution.policy.columns`.  It might be better to use multiple properties instead, such as `execution.policy.indexes` and `execution.policy.columns`




# Configuration

## Configure URL validation

### Set default parameter

|Name|Value|Mutability|
|----|-----|----------|

## Configure Token Validation
### Set default parameter  

## Configure Execution Policy

## Configure Content Policy

## Tests

|Standard Params|JSONParams|# of params|# of policy columns|Use Token?|Qname|
|:-------------:|:--------:|:---------:|:-----------------:|:--------:|:----|
|√||0|0||YADA TEST sec zero params zero polcols|
||√|0|0||YADA TEST sec zero params zero polcols|
|√||1|0||YADA TEST sec one param zero polcols|
||√|1|0||YADA TEST sec one param zero polcols|
|√||1|1||YADA TEST sec one param one derived polcol|
||√|1|1||YADA TEST sec one param one derived polcol|
|√||1|1|√|YADA TEST sec one param one polcol use token|
||√|1|1|√|YADA TEST sec one param one polcol use token|
|√||0|1|√|YADA TEST sec zero params one polcol use token|
||√|0|1|√|YADA TEST sec zero params one polcol use token|
|√||1|0||YADA TEST sec multi params no polcols|
||√|1|0||YADA TEST sec multi params no polcols|
|√||>1|1||YADA TEST sec multi params one polcol|
||√|>1|1||YADA TEST sec multi params one polcol|
|√||>1|1|√|YADA TEST sec multi params one polcol use token|
||√|>1|1|√|YADA TEST sec multi params one polcol use token|
|√||>1|>1||YADA TEST sec multi params multi polcol|
||√|>1|>1||YADA TEST sec multi params multi polcol|
|√||>1|>1|√|YADA TEST sec multi params multi polcol use token|
||√|>1|>1|√|YADA TEST sec multi params multi polcol use token|
