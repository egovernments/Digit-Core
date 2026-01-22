import React from "react";
import { BatchPrediction } from "./BatchPrediction";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BatchPrediction",
  component: BatchPrediction,
};

export const Default = () => <BatchPrediction />;
export const Fill = () => <BatchPrediction fill="blue" />;
export const Size = () => <BatchPrediction height="50" width="50" />;
export const CustomStyle = () => <BatchPrediction style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BatchPrediction className="custom-class" />;

export const Clickable = () => <BatchPrediction onClick={()=>console.log("clicked")} />;

const Template = (args) => <BatchPrediction {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
