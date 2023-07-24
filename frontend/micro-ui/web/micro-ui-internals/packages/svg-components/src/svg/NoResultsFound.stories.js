import React from "react";
import PropTypes from "prop-types";
import { NoResultsFound } from "./NoResultsFound"; // Adjust the import path as per your project structure

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "NoResultsFound",
  component: NoResultsFound,
};

export const Default = () => <NoResultsFound />;
export const Fill = () => <NoResultsFound fill="blue" />;
export const Size = () => <NoResultsFound height="50" width="50" />;
export const CustomStyle = () => <NoResultsFound style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NoResultsFound className="custom-class" />;

export const Clickable = () => <NoResultsFound onClick={() => console.log("clicked")} />;

const Template = (args) => <NoResultsFound {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
