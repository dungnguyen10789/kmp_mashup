import os

def tree(dir, prefix=""):
    contents = list(os.listdir(dir))
    for idx, item in enumerate(contents):
        if item.startswith('build') or item.startswith('.'):
            continue
        path = os.path.join(dir, item)
        connector = "├── " if idx < len(contents)-1 else "└── "
        print(prefix + connector + item)
        if os.path.isdir(path):
            extension = "│   " if idx < len(contents)-1 else "    "
            tree(path, prefix + extension)

tree(".")