#!/usr/bin/env python3
"""
Script to add comprehensive KDoc documentation to Kotlin files
This script analyzes Kotlin files and adds professional documentation where missing
"""

import os
import re
from pathlib import Path

# Root directory to process
root_dir = '/home/graysoncomrademsiska/Documents/Development/doritaas-app last phase/app/src/main/java'

def has_kdoc(lines, start_index):
    """Check if there's already a KDoc comment before the given line"""
    if start_index == 0:
        return False
    
    # Look backwards for KDoc
    for i in range(start_index - 1, max(0, start_index - 10), -1):
        line = lines[i].strip()
        if line.startswith('/**'):
            return True
        if line and not line.startswith('//') and not line.startswith('*') and not line.startswith('*/'):
            return False
    return False

def extract_function_info(line):
    """Extract function name and parameters from function declaration"""
    # Match: fun functionName(param1: Type, param2: Type): ReturnType
    match = re.search(r'fun\s+(\w+)\s*\((.*?)\)', line)
    if match:
        func_name = match.group(1)
        params = match.group(2)
        return func_name, params
    return None, None

def extract_class_info(line):
    """Extract class/data class/enum/object name"""
    # Match class, data class, enum class, sealed class, object, etc.
    match = re.search(r'(?:data\s+|enum\s+|sealed\s+|abstract\s+|open\s+)?(?:class|object|interface)\s+(\w+)', line)
    if match:
        return match.group(1)
    return None

def is_enum_class(line):
    """Check if line declares an enum class"""
    return 'enum class' in line

def is_data_class(line):
    """Check if line declares a data class"""
    return 'data class' in line

def is_interface(line):
    """Check if line declares an interface"""
    return 'interface ' in line

def is_object(line):
    """Check if line declares an object"""
    return re.search(r'\bobject\s+\w+', line) is not None

def generate_function_kdoc(func_name, params, is_composable=False):
    """Generate KDoc for a function"""
    doc = ["/**"]
    
    # Add description based on function name
    if is_composable:
        # Convert camelCase to readable format
        readable_name = re.sub(r'([A-Z])', r' \1', func_name).strip()
        doc.append(f" * {readable_name} Composable")
        doc.append(" *")
        doc.append(" * A composable function that renders the UI for this component.")
    else:
        doc.append(f" * {func_name}")
        doc.append(" *")
        doc.append(" * TODO: Add detailed description of what this function does.")
    
    # Add parameter documentation
    if params and params.strip():
        doc.append(" *")
        param_list = [p.strip() for p in params.split(',') if p.strip()]
        for param in param_list:
            # Handle default values and complex types
            param_parts = param.split('=')[0].split(':')
            if len(param_parts) >= 2:
                param_name = param_parts[0].strip()
                param_type = param_parts[1].strip()
                doc.append(f" * @param {param_name} The {param_name} parameter")
    
    doc.append(" */")
    return doc

def generate_class_kdoc(class_name, line):
    """Generate KDoc for a class"""
    doc = ["/**"]
    
    if is_data_class(line):
        doc.append(f" * {class_name}")
        doc.append(" *")
        doc.append(" * Data class representing [TODO: Add description]")
    elif is_enum_class(line):
        doc.append(f" * {class_name}")
        doc.append(" *")
        doc.append(" * Enum class defining [TODO: Add description]")
    elif is_interface(line):
        doc.append(f" * {class_name}")
        doc.append(" *")
        doc.append(" * Interface defining [TODO: Add description]")
    elif is_object(line):
        doc.append(f" * {class_name}")
        doc.append(" *")
        doc.append(" * Singleton object for [TODO: Add description]")
    else:
        doc.append(f" * {class_name}")
        doc.append(" *")
        doc.append(" * TODO: Add class description")
    
    doc.append(" */")
    return doc

def process_file(filepath):
    """Add documentation to a Kotlin file"""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return False
    
    new_lines = []
    i = 0
    changes_made = False
    
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        
        # Check for @Composable annotation
        is_composable = '@Composable' in stripped
        
        # Check for function declarations
        if stripped.startswith('fun ') or (i > 0 and '@Composable' in lines[i-1]):
            if not has_kdoc(lines, i):
                func_name, params = extract_function_info(line)
                if func_name:
                    # Add KDoc
                    indent = len(line) - len(line.lstrip())
                    kdoc = generate_function_kdoc(func_name, params, is_composable)
                    for doc_line in kdoc:
                        new_lines.append(' ' * indent + doc_line + '\n')
                    changes_made = True
        
        # Check for class declarations
        elif 'class ' in stripped or 'object ' in stripped or 'interface ' in stripped:
            if not stripped.startswith('//'):
                if not has_kdoc(lines, i):
                    class_name = extract_class_info(line)
                    if class_name:
                        # Add KDoc
                        indent = len(line) - len(line.lstrip())
                        kdoc = generate_class_kdoc(class_name, line)
                        for doc_line in kdoc:
                            new_lines.append(' ' * indent + doc_line + '\n')
                        changes_made = True
        
        new_lines.append(line)
        i += 1
    
    # Write back if changes were made
    if changes_made:
        try:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.writelines(new_lines)
            print(f"✓ Added documentation to: {filepath}")
            return True
        except Exception as e:
            print(f"Error writing {filepath}: {e}")
            return False
    
    return False

def main():
    """Main function to process all Kotlin files"""
    print("Adding comprehensive documentation to Kotlin files...")
    print(f"Root directory: {root_dir}\n")
    
    files_processed = 0
    files_updated = 0
    
    # Walk through all .kt files
    for root, dirs, files in os.walk(root_dir):
        # Skip build directories
        if 'build' in root or 'generated' in root:
            continue
            
        for file in files:
            if file.endswith('.kt'):
                filepath = os.path.join(root, file)
                files_processed += 1
                
                if process_file(filepath):
                    files_updated += 1
    
    print(f"\n{'='*60}")
    print(f"Documentation addition complete!")
    print(f"Files processed: {files_processed}")
    print(f"Files updated: {files_updated}")
    print(f"{'='*60}")
    print("\nNOTE: All TODO comments should be replaced with actual descriptions.")

if __name__ == '__main__':
    main()
