# Duty Tracker — Overview

## What is it?

Duty Tracker is a local tool for engineers to track on-call shifts and incident work during their duty period, and generate a ready-to-submit report for HR compensation registration in MyHR.

## The problem

After every on-call week, engineers must manually submit their hours to MyHR. This involves:

- Logging each standby day with the correct hours and rate (weekday vs. Sunday/holiday)
- Logging each overtime hour from incidents, separate from the standby allowance
- Applying the correct allowance percentage per time slot (varies by day and hour)
- Getting all of this right — mistakes mean under- or over-reporting compensation

This is tedious, error-prone, and hard to reconstruct from memory after a busy week.

## The solution

Duty Tracker runs on your machine throughout your on-call period. You log shifts and incidents as they happen. When the period ends, the tool generates a structured report that maps directly to what you need to enter in MyHR — no guesswork, no retroactive reconstruction.

The tool does **not** submit anything to MyHR. Submission still requires manual entry and approval from your Lead Engineering. Duty Tracker ensures you arrive at that step with accurate, complete data.

## Scope

This tool applies to **internal employees** only. Freelancer compensation follows a separate process (see [Confluence](https://jumbo-supermarkten.atlassian.net/wiki/spaces/INC/pages/3617262549)).

## Reference

The on-call allowance policy is documented on the [On-call and overtime registration](https://jumbo-supermarkten.atlassian.net/wiki/spaces/INC/pages/3234431405) Confluence page. The official policy document is hosted on SharePoint under *Arbeidsvoorwaarden en Regelingen*.
