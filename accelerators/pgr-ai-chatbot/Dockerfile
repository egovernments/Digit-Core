# Use an official Python runtime as a parent image
# Use an official Python runtime as a parent image
FROM python:3.10-slim

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the entire project into the container
COPY . /usr/src/app

# Install any needed packages specified in requirements.txt
COPY requirements.txt /usr/src/app/

RUN pip3 install --no-cache-dir -r requirements.txt

# Run main.py when the container launches
CMD ["python", "./main.py"]
