---
name: ADR-014
title: "Drive distance calculation: derive from odometer delta vs. log begin+end odometer"
date: 2026-07-18
status: accepted
domain: backend
agent: Claude Sonnet 5
decisionBy: TBD
---

### Context

`Drive` currently stores a single `odometer` value per drive — the vehicle's total odometer reading at drive end (see `Drive` class Javadoc). Per-drive distance is not stored; it must be derived at query time as the difference between a drive's `odometer` and the next-lowest `odometer` for the same `car_id` (ordered by `drive_date`/insertion).

This works for a single, gapless sequence of drives per car, but if one drive is not logged it carries over to the next driver giving him more distance than actually driven.

Two options are on the table:

### Option 1 — Keep end-odometer-only, derive distance from delta (status quo)

Distance for a drive = `this.odometer - previous.odometer` (previous = next-lowest odometer for the same car).

- ✅ No schema change; simplest possible model; already implemented.
- ⚠️ Any unlogged mileage between two logged drives is invisibly folded into the later drive's "distance," misattributing it to that drive's driver.

### Option 2 — Add a begin-odometer field per drive

Log both `odometerStart` and `odometer` (end) per drive.

- ✅ Each drive's own distance (`odometer - odometerStart`) is precise and independent of logging order — no dependency on "the next row for this car."
- ✅ Any gap between one drive's `odometerStart` and the previous drive's end `odometer` is an explicit, visible "unlogged distance" instead of being silently merged into someone else's drive.
- ⚠️ Requires deciding how to attribute that unlogged gap: e.g. split evenly across all drivers who used the car in the gap window, attribute to no one, or attribute to the car's owner. This needs its own rule/algorithm and likely a small allocation table or computed view.
- ⚠️ Schema change: new nullable-or-required `odometer_start` column, migration, and validation that `odometer_start <= odometer`.
- ⚠️ Slightly more input burden on the user (two readings instead of one) — though many drivers already know their start reading from the previous drive's end reading.

### Decision

Option 1 is chosen due to easier use and implementation.

### Consequences

- Pending a decision, distance/cost-per-km reporting remains only as accurate as the "next-lowest odometer" heuristic in Option 1, with the known misattribution risk described above.
- The first Milage per car has to be the start Milage since it is not included into the calculation, because of a missing lower entry.
