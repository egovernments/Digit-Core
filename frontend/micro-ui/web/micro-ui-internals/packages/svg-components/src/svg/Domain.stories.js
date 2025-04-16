import React from "react";
import { Domain } from "./Domain";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Domain",
  component: Domain,
};

export const Default = () => <Domain />;
export const Fill = () => <Domain fill="blue" />;
export const Size = () => <Domain height="50" width="50" />;
export const CustomStyle = () => <Domain style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Domain className="custom-class" />;

export const Clickable = () => <Domain onClick={()=>console.log("clicked")} />;

const Template = (args) => <Domain {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
