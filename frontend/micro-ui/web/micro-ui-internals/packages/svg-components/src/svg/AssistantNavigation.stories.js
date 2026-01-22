import React from "react";
import { AssistantNavigation } from "./AssistantNavigation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AssistantNavigation",
  component: AssistantNavigation,
};

export const Default = () => <AssistantNavigation />;
export const Fill = () => <AssistantNavigation fill="blue" />;
export const Size = () => <AssistantNavigation height="50" width="50" />;
export const CustomStyle = () => <AssistantNavigation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AssistantNavigation className="custom-class" />;
export const Clickable = () => <AssistantNavigation onClick={()=>console.log("clicked")} />;

const Template = (args) => <AssistantNavigation {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
