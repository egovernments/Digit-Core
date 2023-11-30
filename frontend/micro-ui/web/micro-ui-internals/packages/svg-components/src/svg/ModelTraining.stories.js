import React from "react";
import { ModelTraining } from "./ModelTraining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "ModelTraining",
  component: ModelTraining,
};

export const Default = () => <ModelTraining />;
export const Fill = () => <ModelTraining fill="blue" />;
export const Size = () => <ModelTraining height="50" width="50" />;
export const CustomStyle = () => <ModelTraining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <ModelTraining className="custom-class" />;

export const Clickable = () => <ModelTraining onClick={()=>console.log("clicked")} />;

const Template = (args) => <ModelTraining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
