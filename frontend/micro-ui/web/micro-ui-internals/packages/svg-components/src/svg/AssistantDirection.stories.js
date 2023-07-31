import React from "react";
import { AssistantDirection } from "./AssistantDirection";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssistantDirection",
  component: AssistantDirection,
};

export const Default = () => <AssistantDirection />;
export const Fill = () => <AssistantDirection fill="blue" />;
export const Size = () => <AssistantDirection height="50" width="50" />;
export const CustomStyle = () => <AssistantDirection style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssistantDirection className="custom-class" />;
export const Clickable = () => <AssistantDirection onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssistantDirection {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
