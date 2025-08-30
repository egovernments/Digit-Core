import React from "react";
import { Topic } from "./Topic";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Topic",
  component: Topic,
};

export const Default = () => <Topic />;
export const Fill = () => <Topic fill="blue" />;
export const Size = () => <Topic height="50" width="50" />;
export const CustomStyle = () => <Topic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Topic className="custom-class" />;
export const Clickable = () => <Topic onClick={()=>console.log("clicked")} />;

const Template = (args) => <Topic {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
