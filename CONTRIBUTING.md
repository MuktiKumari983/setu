# Contributing

Thanks for contributing to the **Societal Innovation Collaboration Platform** (SIH26043)!

This prototype is developed by a team, so we keep strict module separation so members can work in parallel **without rewriting each other's code**.

## Module ownership

| Module | Location | Branch prefix |
|--------|----------|---------------|
| Frontend (dashboards, UI) | `frontend/` | `feature/frontend-*` |
| Problem module | `backend/.../problem/` | `feature/problem-module` |
| AI analysis | `ai-service/` | `feature/ai-analysis` |
| University matching | `backend/.../match/` | `feature/university-matching` |
| Student-team matching | `backend/.../match/` + `ai-service` | `feature/student-matching` |
| Industry matching | `backend/.../match/` + `ai-service` | `feature/industry-matching` |
| Project lifecycle | `backend/.../project/` | `feature/project-lifecycle` |
| Failure recovery | `backend/.../project/` + `ai-service` | `feature/failure-recovery` |
| Impact dashboard | `backend/.../impact/` + `frontend` | `feature/impact-dashboard` |
| Authentication | `backend/.../security/`, `user/` | `feature/authentication` |

## Workflow

1. **Clone the repository**

   ```bash
   git clone git@github.com:your-org/sih26043-societal-innovation-platform.git
   cd sih26043-societal-innovation-platform
   cp .env.example .env
   ```

2. **Create a feature branch**

   ```bash
   git checkout -b feature/problem-module
   ```

   Branch naming: `feature/<area>` or `fix/<area>`.
   Examples: `feature/ai-analysis`, `feature/university-matching`, `fix/problem-validation`.

3. **Pull the latest `main`** before you start

   ```bash
   git fetch origin
   git rebase origin/main
   ```

4. **Implement your feature** — only touch files inside your module. Do **not** reformat or refactor code outside your area.

5. **Run the tests** for the space you touched

   ```bash
   # backend
   cd backend && mvn test

   # ai-service
   cd ai-service && python -m pytest

   # frontend
   cd frontend && npm run build
   ```

6. **Commit small, meaningful changes**

   ```bash
   git add <files>
   git commit -m "feat: add problem submission validation"
   ```

   Use conventional-commit style prefixes:
   `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `chore:`.

   Good examples:
   - `feat: add AI problem classification`
   - `feat: add university matching engine`
   - `feat: add project lifecycle status transitions`
   - `fix: validate problem submission fields`
   - `docs: update API documentation`

   Avoid one giant commit for the entire project.

7. **Push** and open a **Pull Request**

   ```bash
   git push -u origin feature/problem-module
   ```

   In the PR description: what you changed, how it was tested, and (if relevant) a screenshot.

8. **Handle review feedback** — make follow-up commits on the same branch (never force-push in review).

## Rules

- **Never directly rewrite another developer's work.** If a change is needed in their module, coordinate via an issue / PR and let them do it.
- Never commit `.env`, secrets, or credentials (`.gitignore` handles `.env`).
- Keep commits focused and atomic.
- Run `mvn test` / `python -m pytest` / `npm run build` before opening a PR.
- When in doubt, ask — this is a collaborative SIH breakout, communication wins.