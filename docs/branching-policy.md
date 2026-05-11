# 🔀 Branching Policy

## Branch Naming

```
surname/feature
```

## Workflow

```bash
git checkout -b surname/feature
git push origin surname/feature
```

## PR Rules

- PRs must target `staging` only
- `staging` → `main`
- All PRs must go to `staging`. Direct PRs to `main` are **not allowed**.

## Why `staging` Exists

- Acts as an integration layer for features
- Prevents breaking `main`
- Ensures only tested code reaches releases

## Flow

```
feature/surname/feature → staging → main
```
