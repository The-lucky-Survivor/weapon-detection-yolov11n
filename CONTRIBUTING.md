# Contributing to YOLOv11 Weapon Detection

Thank you for your interest in contributing. This document describes how to
report issues, suggest improvements, and submit code changes.

---

## Ways to Contribute

- **Bug reports** — open an issue with a minimal reproduction case
- **Evaluation results** — contribute test-set metrics or confusion matrices
- **Inference improvements** — improve the portable inference script
- **Dataset documentation** — help clarify dataset sources and licences
- **Android application** — contribute or link the Android front-end

---

## Development Setup

```bash
git clone https://github.com/The-lucky-Survivor/weapon-detection-yolov11n.git
cd weapon-detection-yolov11n

python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # Linux / macOS

pip install torch torchvision --index-url https://download.pytorch.org/whl/cpu
pip install -r requirements.txt
```

---

## Code Style

- Follow [PEP 8](https://pep8.org/) for Python files.
- Add docstrings to new functions and modules.
- Do not introduce machine-specific absolute paths into scripts. Use `argparse`,
  `pathlib.Path(__file__)`, or environment variables.
- Clear Jupyter notebook outputs before committing (`Cell > All Output > Clear`).

---

## Commit Messages

Use the [Conventional Commits](https://www.conventionalcommits.org/) format:

```
feat: add batch inference support to infer.py
fix: resolve OBB conversion edge case in convert.py
docs: update dataset download instructions in README
chore: add ensemble-boxes to requirements.txt
```

---

## Pull Request Checklist

Before opening a PR, confirm that:

- [ ] The code runs without errors in a clean virtual environment
- [ ] No secrets, API keys, or absolute local paths are introduced
- [ ] No model weights (`.pt`, `.onnx`) or large files are staged
- [ ] Jupyter notebook outputs are cleared
- [ ] The README is updated if public-facing behaviour changes
- [ ] The `.gitignore` remains intact

---

## What NOT to Contribute

- Personal or sensitive data
- Datasets or model weights without clear redistribution rights
- Claims about model performance that have not been measured
- Unnecessary dependencies or large binary files

---

## Reporting Issues

Open a GitHub Issue and include:

1. A short, descriptive title
2. Steps to reproduce the problem
3. The full error message or unexpected output
4. Your Python version and OS

---

## Licence

By contributing, you agree that your contributions will be licensed under the
[MIT Licence](LICENSE) that covers this repository.
