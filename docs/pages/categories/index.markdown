---
layout: default
title: Uncommon SQL Changes
has_children: true
---

# Uncommon SQL Changes

The following pages includes information on how we remove/add/modify SQLancer codes to implement differential testing to measure how applicable differential testing is. It does not directly reflect the DBMSs' SQL implementations, some features are not yet implemented in SQLancer, so they were not recorded in these pages. 

**ID** - unique identifier of a category

**Name** - name of the category

**Difference** - reason why we change them, either because they cause error in other DBMSs (syntactic), or the DBMSs handle them differenly (semantic)

**Action Taken** - what we did to handle the difference (add, remove, or modify code)

**DBMS** - which DBMS's implementation in SQLancer is being changed

**Description** - additional information on the changes