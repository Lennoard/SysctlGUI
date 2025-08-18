# Translating SysctlGUI

Thank you for your interest in translating SysctlGUI! Your contributions help make the app accessible to a wider audience.

## How to Contribute

1.  **Fork the Repository:** Start by forking the [SysctlGUI repository](https://github.com/your-github-username/SysctlGUI) (replace `your-github-username/SysctlGUI` with the actual repository URL) to your own GitHub account.
2.  **Clone Your Fork:** Clone your forked repository to your local machine.
    ```bash
    git clone https://github.com/YOUR_USERNAME/SysctlGUI.git
    cd SysctlGUI
    ```
3.  **Create a New Branch:** Create a new branch for your translation.
    ```bash
    git checkout -b translate-yourlanguage
    ```
4.  **Translate the Files:**
    *   **String Resources:** These are the primary files for translation.
        *   `app/src/main/res/values/strings.xml`
        *   `app/src/main/res/values/params_info.xml`
        *   `data/src/main/res/values/strings.xml`

        To translate these files, create a new `values-xx` directory in the same `res` folder, where `xx` is the ISO 639-1 code for the language you are translating to (e.g., `values-es` for Spanish, `values-de` for German). Then, copy the original `strings.xml` or `params_info.xml` into this new directory and translate the string values within the XML tags.

        **Example (strings.xml for Spanish):**
        Create `app/src/main/res/values-es/strings.xml` and translate the content, preserving special format tags (%s, %1$s, etc)
        ```xml
        <resources>
            <string name="app_name">SysctlGUI</string>
            <!-- Translate this -->
            <string name="undo">Undo</string>
            <string name="selected_file_format">Selected file: %s</string>
            <!-- To this -->
            <string name="undo">Deshacer</string>
            <string name="selected_file_format">Archivo seleccionado: %s</string>
        </resources>
        ```

    *   **Raw Text Files (Optional):** If you feel brave, you can also translate the `.txt` files located in `data/src/main/res/raw/`.
        *   When translating these files, it is **crucial** to respect their original format. These files often have a specific structure that the app relies on.
        *   Translate the text content, but leave any special characters, newlines, or formatting intact.
        *   Place the translated `.txt` files in a new `raw-xx` directory within `data/src/main/res/` (e.g., `data/src/main/res/raw-es/` for Spanish).

5.  **Commit Your Changes:** Commit your translated files with a clear commit message.
    ```bash
    git add .
    git commit -m "Add translation to [Your Language]"
    ```
6.  **Push to Your Fork:** Push your changes to your forked repository.
    ```bash
    git push origin translate-yourlanguage
    ```
7.  **Create a Pull Request:** Go to the original SysctlGUI repository on GitHub and create a new Pull Request from your forked branch. Provide a clear description of your changes.

## Important Notes

*   Ensure your translations are accurate and natural-sounding in the target language.
*   Do not translate resource names (e.g., `app_name` in `<string name="app_name">`). Only translate the text content between the XML tags.
*   For `.txt` files, preserving the exact original formatting is critical for the app to function correctly with the translated content.
*   If you are unsure about any part of the translation process, feel free to open an issue on the main repository to ask for clarification.

Thank you for your contribution!
