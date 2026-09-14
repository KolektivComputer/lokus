# CI dual-publish (GH Packages npm + JSR)

Token used to open this PR cannot edit `.github/workflows/*` (missing `workflows` scope). Apply manually:

```yaml
permissions:
  contents: read
  packages: write
  id-token: write  # JSR OIDC
```

npm → GitHub Packages:
- `registry-url: https://npm.pkg.github.com`
- `scope: "@kolektiv"`
- `NODE_AUTH_TOKEN: ${{ secrets.GITHUB_TOKEN }}`

Keep existing Yuri Capital npm publish as dual.

JSR: claim `@kolektiv` on jsr.io, link package for OIDC, then `npx jsr publish`.

Epic: https://github.com/KolektivComputer/.github/issues/2


Also set Gradle group to `computer.kolektiv.lokus` (see #18).
