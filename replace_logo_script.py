import os
import glob

# Client files
client_dir = r"e:\TFG\TFGActualizado\TrackerGym\src\main\resources\templates\cliente"
for file_path in glob.glob(os.path.join(client_dir, "*.html")):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    new_content = content.replace('<div class="navbar-brand">💪 TrackerGym</div>', '<div class="navbar-brand"><img th:src="@{/img/logosvg.svg}" alt="Logo" class="nav-logo"> TrackerGym</div>')
    new_content = new_content.replace('<div class="navbar-brand">  💪 TrackerGym</div>', '<div class="navbar-brand"><img th:src="@{/img/logosvg.svg}" alt="Logo" class="nav-logo"> TrackerGym</div>')
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)

# Trainer files
entrenador_dir = r"e:\TFG\TFGActualizado\TrackerGym\src\main\resources\templates\entrenador"
old1 = '''        <div class="logo-area">
            <h2>TrackerGym</h2>
        </div>'''
new1 = '''        <div class="logo-area">
            <img th:src="@{/img/logosvg.svg}" alt="Logo" class="sidebar-logo">
            <h2>TrackerGym</h2>
        </div>'''
old2 = '''        <div class="logo">
            <h2>TrackerGym</h2>
        </div>'''
new2 = '''        <div class="logo">
            <img th:src="@{/img/logosvg.svg}" alt="Logo" class="sidebar-logo">
            <h2>TrackerGym</h2>
        </div>'''

for file_path in glob.glob(os.path.join(entrenador_dir, "*.html")):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    new_content = content.replace(old1, new1)
    new_content = new_content.replace(old2, new2)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(new_content)
