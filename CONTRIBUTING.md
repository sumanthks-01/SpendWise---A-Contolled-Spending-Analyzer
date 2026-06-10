# Contributing to SpendWise

Thank you for your interest in contributing to SpendWise! We welcome bug reports, feature requests, and pull requests.

## How to Contribute

- Fork the repository and create a feature branch (`git checkout -b feature/your-feature`).
- Follow existing code style and conventions.
- Write tests for new features and ensure all tests pass.
- Keep commits focused and descriptive.
- Open a Pull Request describing the change, motivation, and any migration steps.

## Code Style

- Java code follows standard conventions (use the project's formatter).
- Keep methods small and focused; prefer readability.

## Branching and Releases

- Use `main` for releases and `develop` for ongoing work (adjust if repository uses different flow).
- Prefix feature branches with `feature/`, bugfixes with `fix/`, and hotfixes with `hotfix/`.

## Testing

- Unit tests must be added for business logic changes.
- Run `mvn test` before submitting a PR.

## CI

- Pull requests will be validated by CI (build, tests, basic checks). Fix failures before merging.

## Reviewing

- PRs should include a clear description and testing steps.
- Address review comments promptly; squash or rebase as requested.

## Security

If you discover a security vulnerability, see SECURITY.md for reporting instructions.
