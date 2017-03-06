## Documentation sources

Documentation build with [mkdoks](http://www.mkdocs.org/).

### Prerequisites

* mkdocs 0.16.1
* [mkdocs-material](http://squidfunk.github.io/mkdocs-material/) 1.0.3

On windows use [chocolately](https://chocolatey.org) for installation:

```bash
choco install mkdocs
choco install mkdocs-material
```

Otherwise, look [docs](http://www.mkdocs.org/#installation).

For code blocks [syntax highlighting pygments](http://squidfunk.github.io/mkdocs-material/extensions/codehilite/) required:

```bash
pip install pygments
```

For [advanced markdown syntax features](http://squidfunk.github.io/mkdocs-material/extensions/pymdown) install extensions:

```bash
pip install pymdown-extensions
```

### Dev mode

To start live reload server:

```bash
mkdocs serve
```

Open [http://localhost:8000](http://localhost:8000)

### Dist

To build site:

```bash
mkdocs build --clean
```

Site will be generated in `site` folder.
