from setuptools import setup, find_packages

setup(
    name='digit_client',
    version='0.1',
    packages=find_packages(),
    install_requires=[
        'requests',
    ],
    description='Python client for DIGIT services',
    author='eGov Foundation',
    author_email='your.email@example.com',
    url='https://github.com/yourusername/digit_client',
)