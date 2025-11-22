# Push to GitHub - Step by Step Guide

## Step 1: Create a GitHub Repository

1. Go to [GitHub.com](https://github.com) and sign in
2. Click the **"+"** icon in the top right corner
3. Select **"New repository"**
4. Fill in:
   - **Repository name**: `flyra` (or any name you prefer)
   - **Description**: "Spring Boot leader election system with coordinator and application pods"
   - **Visibility**: Choose Public or Private
   - **DO NOT** initialize with README, .gitignore, or license (we already have these)
5. Click **"Create repository"**

## Step 2: Push Your Code

After creating the repository, GitHub will show you commands. Use these:

```bash
# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/flyra.git

# Or if using SSH (recommended if you have SSH keys set up):
# git remote add origin git@github.com:YOUR_USERNAME/flyra.git

# Push to GitHub
git branch -M main
git push -u origin main
```

## Quick Commands (All in One)

Replace `YOUR_USERNAME` with your actual GitHub username:

```bash
cd /Users/aniketgajare/flyra
git remote add origin https://github.com/YOUR_USERNAME/flyra.git
git branch -M main
git push -u origin main
```

## If You Need to Set Git Config First

```bash
# Set your name (if not already set)
git config --global user.name "Your Name"

# Set your email (if not already set)
git config --global user.email "your.email@example.com"
```

## Verify

After pushing, visit:
`https://github.com/YOUR_USERNAME/flyra`

You should see all your files there!

## Future Updates

When you make changes and want to push:

```bash
git add .
git commit -m "Your commit message"
git push
```

